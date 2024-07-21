import requests
import json
import cv2
from pyzbar import pyzbar
from tkinter import Tk, Button, Entry, Label, messagebox
from pydub import AudioSegment
from pydub.playback import play
import numpy as np  # Import numpy
import time

# API URLs
BASE_URL = "http://localhost:8080/api/v1/organizer"
LOGIN_URL = f"{BASE_URL}/login"
LOGOUT_URL = f"{BASE_URL}/logout"
CHECKIN_URL = f"{BASE_URL}/checkin"

# Global token variable
token = None
stop_check_in = False


# QR code scanner function
def scan_qr_code():
    global stop_check_in
    cap = cv2.VideoCapture(0)
    qr_code_data = None

    while True:
        ret, frame = cap.read()
        if not ret:
            break
        decoded_objects = pyzbar.decode(frame)
        for obj in decoded_objects:
            qr_code_data = obj.data.decode('utf-8')
            break
        cv2.imshow('QR Code Scanner', frame)
        if qr_code_data:
            break
        if cv2.waitKey(1) & 0xFF == ord('q'):
            stop_check_in = True
            break

    cap.release()
    cv2.destroyAllWindows()

    return qr_code_data


# Login function
def login(username, password, familiar_name, ip_address):
    global token
    payload = {
        "username": username,
        "password": password,
        "familiar_name": familiar_name,
        "ip_address": ip_address
    }
    response = requests.post(LOGIN_URL, json=payload)
    if response.status_code == 200:
        token = response.json().get("token")
        messagebox.showinfo("Login", "Login successful")
        start_check_in_loop()
    else:
        messagebox.showerror("Login", "Login failed")


# Logout function
def logout():
    global token
    payload = {
        "token": token
    }
    response = requests.post(LOGOUT_URL, json=payload)
    if response.status_code == 200:
        token = None
        messagebox.showinfo("Logout", "Logout successful")
    else:
        messagebox.showerror("Logout", "Logout failed")


# Check-in function
def check_in(qr_code):
    event_id = event_id_entry.get()
    payload = {
        "token": token,
        "qrCode": qr_code,
        "eventId": event_id
    }
    response = requests.post(CHECKIN_URL, json=payload)
    data = response.json()
    if response.status_code == 200:
        generate_beep(frequency=1000, duration=175)  # one fast beep for success
        formatted_data = format_checkin_data(data)
        messagebox.showinfo("Check-In", f"Check-in successful:\n{formatted_data}")
    else:
        for _ in range(2):
            generate_beep(frequency=360, duration=125)
        formatted_data = format_checkin_data(data)
        messagebox.showerror("Check-In", f"Check-in failed:\n{formatted_data}")


# Format check-in data
def format_checkin_data(data):
    formatted_data = ""
    for key, value in data.items():
        formatted_key = ' '.join([word.upper() for word in key.split('_')])
        formatted_data += f"{formatted_key}: {value}\n"
    return formatted_data


# Generate custom beep sound
def generate_beep(frequency, duration):
    sample_rate = 44100  # Hertz
    t = duration / 1000.0  # convert duration to seconds
    samples = (np.sin(2 * np.pi * np.arange(sample_rate * t) * frequency / sample_rate)).astype(np.float32)

    # Use pydub to create an AudioSegment
    audio_segment = AudioSegment(
        samples.tobytes(),
        frame_rate=sample_rate,
        sample_width=samples.dtype.itemsize,
        channels=1
    )

    # Play the sound
    play(audio_segment)


# Start check-in loop function
def start_check_in_loop():
    global stop_check_in
    stop_check_in = False
    while not stop_check_in:
        qr_code = scan_qr_code()
        if qr_code:
            check_in(qr_code)
        else:
            messagebox.showerror("QR Code", "Failed to scan QR code")
    stop_check_in_loop()


# Stop check-in loop function
def stop_check_in_loop():
    global stop_check_in
    stop_check_in = True
    logout()


# Create GUI
def create_gui():
    root = Tk()
    root.title("Event Organizer")

    Label(root, text="Username").grid(row=0)
    Label(root, text="Password").grid(row=1)
    Label(root, text="Familiar Name").grid(row=2)
    Label(root, text="IP Address").grid(row=3)
    Label(root, text="Event ID").grid(row=4)

    username = Entry(root)
    password = Entry(root, show='*')
    familiar_name = Entry(root)
    ip_address = Entry(root)
    global event_id_entry
    event_id_entry = Entry(root)

    username.grid(row=0, column=1)
    password.grid(row=1, column=1)
    familiar_name.grid(row=2, column=1)
    ip_address.grid(row=3, column=1)
    event_id_entry.grid(row=4, column=1)

    Button(root, text="Login",
           command=lambda: login(username.get(), password.get(), familiar_name.get(), ip_address.get())).grid(row=5,
                                                                                                              column=0)
    Button(root, text="Stop and Logout", command=stop_check_in_loop).grid(row=5, column=1)

    root.mainloop()


if __name__ == "__main__":
    create_gui()
