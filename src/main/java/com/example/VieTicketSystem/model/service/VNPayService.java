package com.example.VieTicketSystem.model.service;

import com.example.VieTicketSystem.config.VNPayConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Original author: https://github.com/pad1092/VNPAY-Springboot-Demo

@Service
public class VNPayService {

    private final ObjectMapper jacksonObjectMapper;
    private final WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    public VNPayService(ObjectMapper jacksonObjectMapper, WebClient.Builder webClientBuilder) {
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.webClientBuilder = webClientBuilder;
        this.webClient = webClientBuilder.baseUrl(VNPayConfig.vnp_APIURL).build();
    }

    // Create order and return payment URL
    public String createOrder(long total, String orderInformation, String urlReturn, String clientIp, Map<String, String> vnp_Params) {
        String vnp_Version = VNPayConfig.vnp_Version;
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomAlphanumericString(64);
        String vnp_IpAddr = clientIp;
        String vnp_TmnCode = VNPayConfig.getVnp_TmnCode();
        String orderType = "190000";

        if (vnp_Params == null) {
            vnp_Params = new HashMap<>();
        }
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(total * 100));
        vnp_Params.put("vnp_CurrCode", VNPayConfig.vnp_CurrCode);

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInformation);
        vnp_Params.put("vnp_OrderType", orderType);

        vnp_Params.put("vnp_Locale", VNPayConfig.vnp_Locale);

        urlReturn += VNPayConfig.vnp_Returnurl;
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(now);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        LocalDateTime expireTime = now.plusMinutes(15);
        String vnp_ExpireDate = formatter.format(expireTime);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.getVnp_HashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    // Handle payment response
    public VNPayStatus orderReturn(Map<String, String> fields) {
        String vnp_SecureHash = fields.get("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = VNPayConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(fields.get("vnp_TransactionStatus"))) {
                return VNPayStatus.SUCCESS;
            } else {
                return VNPayStatus.FAILED;
            }
        } else {
            return VNPayStatus.INVALID;
        }
    }

    // Refund order and return response body as a Mono
    public Mono<String> refundOrder(Map<String, String> transactionData, long refundAmount) throws IOException {
        String vnp_RequestId = VNPayConfig.getRandomAlphanumericString(24);
        String vnp_Version = VNPayConfig.vnp_Version;
        String vnp_Command = "refund";
        String vnp_TmnCode = VNPayConfig.getVnp_TmnCode();
        String vnp_TransactionType = "03";
        String vnp_TxnRef = transactionData.get("vnp_TxnRef");
        String vnp_Amount = String.valueOf(refundAmount * 100);
        String vnp_OrderInfo = "Refund order ID: " + vnp_TxnRef;
        String vnp_TransactionDate = transactionData.get("vnp_CreateDate");
        String vnp_CreateBy = "VieTicket";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(now);

        String vnp_IpAddr = getPublicIP();

        String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionDate, vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.getVnp_HashSecret(), hash_Data);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("vnp_RequestId", vnp_RequestId);
        requestBody.put("vnp_Version", vnp_Version);
        requestBody.put("vnp_Command", vnp_Command);
        requestBody.put("vnp_TmnCode", vnp_TmnCode);
        requestBody.put("vnp_TransactionType", vnp_TransactionType);
        requestBody.put("vnp_TxnRef", vnp_TxnRef);
        requestBody.put("vnp_Amount", vnp_Amount);
        requestBody.put("vnp_OrderInfo", vnp_OrderInfo);
        requestBody.put("vnp_TransactionDate", vnp_TransactionDate);
        requestBody.put("vnp_CreateBy", vnp_CreateBy);
        requestBody.put("vnp_CreateDate", vnp_CreateDate);
        requestBody.put("vnp_IpAddr", vnp_IpAddr);
        requestBody.put("vnp_SecureHash", vnp_SecureHash);

        String jsonRequestBody = jacksonObjectMapper.writeValueAsString(requestBody);

        // Send POST request to API URL with jsonRequestBody as the request body

        return webClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonRequestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    // Process refund response
    public VNPayStatus processResponse(Map<String, String> fields) {
        String vnp_SecureHash = fields.get("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String data = String.join("|",
                fields.get("vnp_ResponseId"),
                fields.getOrDefault("vnp_Command", ""),
                fields.get("vnp_ResponseCode"),
                fields.get("vnp_Message"),
                fields.get("vnp_TmnCode"),
                fields.get("vnp_TxnRef"),
                fields.get("vnp_Amount"),
                fields.get("vnp_BankCode"),
                fields.getOrDefault("vnp_PayDate", ""),
                fields.get("vnp_TransactionNo"),
                fields.get("vnp_TransactionType"),
                fields.get("vnp_TransactionStatus"),
                fields.get("vnp_OrderInfo"),
                fields.getOrDefault("vnp_PromotionCode", ""),
                fields.getOrDefault("vnp_PromotionAmount", "")
        );

        String secretKey = VNPayConfig.getVnp_HashSecret();
        String checksum = VNPayConfig.hmacSHA512(secretKey, data);

        if (!checksum.equals(vnp_SecureHash)) {
            return VNPayStatus.INVALID;
        }

        switch (fields.get("vnp_Command")) {
            case "refund", "querydr" -> {
                if ("00".equals(fields.get("vnp_ResponseCode")) && "00".equals(fields.get("vnp_TransactionStatus"))) {
                    return VNPayStatus.SUCCESS;
                } else {
                    return VNPayStatus.FAILED;
                }
            }
            default -> {
                return VNPayStatus.INVALID;
            }
        }
    }

    // Query order and return response body as a Mono
    public Mono<String> queryTransaction(Map<String, String> transactionData) throws IOException {
        String vnp_RequestId = VNPayConfig.getRandomAlphanumericString(24);
        String vnp_Version = VNPayConfig.vnp_Version;
        String vnp_Command = "querydr";
        String vnp_TmnCode = VNPayConfig.getVnp_TmnCode();
        String vnp_TxnRef = transactionData.get("vnp_TxnRef");
        String vnp_OrderInfo = "Query order ID: " + vnp_TxnRef;
        String vnp_TransactionDate = transactionData.get("vnp_CreateDate");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(now);

        String vnp_IpAddr = getPublicIP();

        String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransactionDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.getVnp_HashSecret(), hash_Data);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("vnp_RequestId", vnp_RequestId);
        requestBody.put("vnp_Version", vnp_Version);
        requestBody.put("vnp_Command", vnp_Command);
        requestBody.put("vnp_TmnCode", vnp_TmnCode);
        requestBody.put("vnp_TxnRef", vnp_TxnRef);
        requestBody.put("vnp_OrderInfo", vnp_OrderInfo);
        requestBody.put("vnp_TransactionDate", vnp_TransactionDate);
        requestBody.put("vnp_CreateDate", vnp_CreateDate);
        requestBody.put("vnp_IpAddr", vnp_IpAddr);
        requestBody.put("vnp_SecureHash", vnp_SecureHash);

        String jsonRequestBody = jacksonObjectMapper.writeValueAsString(requestBody);

        // Send POST request to API URL with jsonRequestBody as the request body
        return webClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonRequestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

    // Get public IP address of the server
    public String getPublicIP() throws IOException {
        URL url = new URL("http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-config/external-ip");
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Metadata-Flavor", "Google");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String publicIP = in.readLine();
        in.close();

        return publicIP;
    }

    public enum VNPayStatus {
        SUCCESS, FAILED, INVALID;
    }
}
