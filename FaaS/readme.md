

Serverless:

✅ Limited writable space: only /tmp/ is writable.
✅ Ephemeral environment: each invocation starts fresh.
✅ File uploads come in-memory via request.files.



"preflight request."

# Upload File to Google Cloud Storage using Google Cloud Functions

Python -> 

gcloud functions deploy uploadFileFunction \
  --runtime python310 \
  --trigger-http \
  --allow-unauthenticated \
  --source . \


Java -> 

gcloud info --run-diagnostics


gcloud functions deploy mkr-file-processor \
  --entry-point com.example.MkrFileProcessor \
  --runtime java21 \
  --trigger-http \
  --gen2 \
  --region=us-central1 \
  --allow-unauthenticated \
  --source . \


  ERROR: (gcloud.functions.deploy) OperationError: code=3, message=Build failed with status: FAILURE and message: function has neither pom.xml nor already-built jar file; directory has these entries: .googlebuild, main.java. For more details see the logs at https://console.cloud.google.com/cloud-build/builds;region=us-central1/b87995aa-5437-4da9-abbb-55a41b897076?project=946555989276.

  Lolek2345!