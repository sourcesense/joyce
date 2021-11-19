import requests
import pytest
import sh
from common import *

# RELEASE note
# JSON logic filter
# rootOnly on schema

@pytest.mark.release130
def test_root_only():
  namespace = test_root_only.__name__

  # create two schemas one with a parent and one without
  parent_schema = load_schema("user-schema", namespace)
  resp1 = save_schema(parent_schema)
    
  assert resp1.status_code == 201
  parent_uri = resp1.json()['schemaUri']

  child_schema = load_schema("user-schema", namespace)
  child_schema['$metadata']['name'] = "user_child"
  child_schema['$metadata']['parent'] = parent_uri

  resp2 = save_schema(child_schema)
  assert resp2.status_code == 201

  resp3 = requests.get(f"{IMPORT_GATEWAY}/schema/import/{namespace}").json()
  assert len(resp3) == 2

  resp4 = requests.get(f"{IMPORT_GATEWAY}/schema/import/{namespace}?root_only=true").json()
  assert len(resp4) == 1
  


@pytest.mark.release130
def test_json_logic():
  namespace = test_json_logic.__name__
  schema = load_schema("user-schema", namespace)
  schema['$metadata']['extra']  = {
    "filter": """{">" : [ { "var" : "user_id" }, 110 ]}"""
  }
  resp = save_schema(schema)
  assert resp.status_code == 201
  
  resp2 = import_doc_dryrun({
            "user_id": 1337,
            "first_name": "Jon",
            "last_name": "Snow",
            "email": "jon@winterfell.cold",
            "state": "Westeros"
        }, resp.json()['schemaUri'])

  assert resp2.status_code == 200 
  assert resp2.json()['processStatus'] == 'IMPORTED'
  
  resp3 = import_doc_dryrun({
            "user_id": 13,
            "first_name": "Mark",
            "last_name": "Snow",
            "email": "mark@winterfell.cold",
            "state": "Westeros"
        }, resp.json()['schemaUri'])

  assert resp3.status_code == 200 
  assert resp3.json()['processStatus'] == 'SKIPPED'


# @pytest.mark.miau
# def test_miau():
#   # kcat -b kafka:9092 -t joyce_notification -o -1 -f 'Topic %t [%p] at offset %o: key %k\n' -e
#   msgs = sh.kcat("-b", "kafka:9092", "-t", "joyce_notification", "-o", "-2", "-e")
#   print("--->", msgs)
#   assert False