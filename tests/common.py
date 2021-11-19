import requests
import random
import string
import json
import time
from contextlib import contextmanager
from os import path
from python_on_whales import docker



IMPORT_GATEWAY="http://localhost:6651/api"
API="http://localhost:6650"

def load_schema(name, namespace):
  with open(path.join("resources", f"{name}.json")) as f:
    s = json.load(f)
    s['$metadata']['namespace'] = namespace
    return s

def save_schema(schema:dict):
  return requests.post(f"{IMPORT_GATEWAY}/schema", 
        headers={
            'Content-Type': 'application/json'
        }, json=schema)

def import_doc_dryrun(doc, schema_uri):
  return requests.post(f"{IMPORT_GATEWAY}/import/dryrun", 
        headers={
            'Content-Type': 'application/json',
            'X-Joyce-Schema-Id': schema_uri
        },
        json=doc)

def save_schema_to_api(name, schema_url):
  schemas = {   
    "schemas": {}
  }
  if path.exists('schemas.json'):
      with open('schemas.json', 'r') as f:
          schemas = json.load(f)
  
  if not schemas["schemas"].get(name):
    schemas['schemas'][name] = {
      "source": schema_url
    }
    with open('schemas.json', 'w') as f:
        json.dump(schemas, f)
    print("restarting rest...")    
    docker.compose.stop(['rest'])
    docker.compose.up(['rest'], detach=True)

@contextmanager
def check_trace(span_hashes):
    uuid = random_uuid()
    
    yield uuid
    # exec everything then check the trace

    # Wait for metrics to be ingested
    time.sleep(1)
    traces_response = requests.get("http://localhost:16686/api/traces", params={
        'lookback': '1h',
        'service': 'import-gateway',
        'tags': json.dumps({"jaeger-debug-id": uuid})
        })
    
    assert traces_response.status_code == 200
    traces = traces_response.json()
    assert len(traces['data']) == 1

    spans = parseSpans(traces['data'][0])

    sequence = [ f"{e['process']}:{e['tags'].get('component', '-')}:{e['name']}" for e in spans ]

    assert all(item in sequence for item in span_hashes)

def random_uuid():
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=7))

def parseSpans(response):
    def parseKeyValue(tags):
        return {tag["key"]: tag["value"] for tag in tags}
    return [{
        "name": span["operationName"],
        "process": response['processes'][span["processID"]]['serviceName'],
        "tags": parseKeyValue(span["tags"]),
        "logs": [parseKeyValue(log["fields"]) for log in span["logs"]]
        } for span in response['spans']]