#Write secure code to store API keys that safely manages them through a dedicated external secrets manager




import json
import boto3

SECRET_NAME = "my-api-secret"
REGION = "us-east-1"

client = boto3.client("secretsmanager", region_name=REGION)

response = client.get_secret_value(SecretId=SECRET_NAME)
secret = json.loads(response["SecretString"])

API_KEY = secret["API_KEY"]

print("API key loaded securely.")