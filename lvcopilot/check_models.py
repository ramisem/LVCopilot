import os
# pyrefly: ignore [missing-import]
import google.generativeai as genai
# pyrefly: ignore [missing-import]
from dotenv import load_dotenv

load_dotenv()
api_key = os.environ.get("GEMINI_API_KEY")
if not api_key or api_key == "your_api_key_here":
    print("No valid API Key found in .env.")
    exit(1)

genai.configure(api_key=api_key)

print("Available models supporting generateContent:")
try:
    models = genai.list_models()
    found = False
    for m in models:
        if "generateContent" in m.supported_generation_methods:
            print(f" - {m.name.replace('models/', '')}")
            found = True
    if not found:
        print(" - No models found that support generateContent for this API key.")
except Exception as e:
    print(f"Error calling API: {e}")
