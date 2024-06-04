import requests
import json
import cv2
from pyzbar.pyzbar import decode

API_BASE_URL = "http://localhost:8080/api/v1/organizer/"

def login(username, password):
    url = API_BASE_URL + "login"
    payload = {"username": username, "password": password}
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        return response.json().get('token')
    else:
        print(f"Login failed: {response.status_code}")
        return None

def logout(token):
    url = API_BASE_URL + "logout"
    payload = {"token": token}
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        print("Logged out successfully")
    else:
        print(f"Logout failed: {response.status_code}")

def query_events(token):
    url = API_BASE_URL + "query-events"
    payload = {"token": token}
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        return response.json()
    else:
        print(f"Query events failed: {response.status_code}")
        return None

def check_in(token, qr_code, event_id):
    url = API_BASE_URL + "checkin"
    payload = {"token": token, "qrCode": qr_code, "eventId": event_id}
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        print("Check-in successful")
        print(response.json())
    else:
        print(f"Check-in failed: {response.status_code}")
        print(response.json())

def scan_qr_code():
    cap = cv2.VideoCapture(0)
    while True:
        ret, frame = cap.read()
        decoded_objects = decode(frame)
        for obj in decoded_objects:
            qr_code = obj.data.decode("utf-8")
            cap.release()
            cv2.destroyAllWindows()
            return qr_code
        cv2.imshow('QR Code Scanner', frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    username = input("Enter username: ")
    password = input("Enter password: ")
    token = login(username, password)
    if token:
        print("Login successful")
        events = query_events(token)
        if events:
            print("Upcoming events:")
            for event in events:
                print(event)
            event_id = input("Enter the event ID for check-in: ")
            while True:
                print("Please scan the QR code of the ticket")
                qr_code = scan_qr_code()
                print(f"QR Code: {qr_code}")
                check_in(token, qr_code, event_id)
                if input("Do you want to check-in another ticket? (y/n): ") == "n":
                    break
        logout(token)
