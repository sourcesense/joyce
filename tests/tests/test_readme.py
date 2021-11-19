import requests
import pytest
import time
from common import *

@pytest.mark.dependency()
def test_save_schema():
    hashes = [
        'import-gateway:java-mongo:update',  
        'import-gateway:java-kafka:To_joyce_schema', 
        'mongodb-sink:java-kafka:To_joyce_notification']

    with check_trace(hashes) as uuid:
        response = requests.post(f"{IMPORT_GATEWAY}/schema", 
        headers={
            'Content-Type': 'application/x-yaml',
            "jaeger-debug-id": uuid
        },
        data=open('resources/import-user.yaml', 'rb'))
    
        assert response.status_code == 201
        assert response.json()['schemaUri'] == 'joyce://schema/import/default.user'
    
@pytest.mark.dependency(depends=['test_save_schema'])
def test_get_schema():
    response = requests.get(f"{IMPORT_GATEWAY}/schema/import/default/user")
    assert response.status_code == 200

    schema = response.json()
    assert schema['uid'] == 'joyce://schema/import/default.user'

@pytest.mark.dependency(depends=['test_save_schema'])
def test_dry_run():
    response = requests.post(f"{IMPORT_GATEWAY}/import/dryrun", 
        headers={
            'Content-Type': 'application/json',
            'X-Joyce-Schema-Id': 'joyce://schema/import/default.user'
        },
        json={
            "user_id": 1337,
            "first_name": "Jon",
            "last_name": "Snow",
            "email": "jon@winterfell.cold",
            "state": "Westeros"
        })
    
    assert response.status_code == 200
    result = response.json()

    assert result['result']['code'] == 1337
    assert result['result']['full_name'] == "JON_SNOW"

@pytest.mark.slow
def test_import():
    response = requests.post(f"{IMPORT_GATEWAY}/import", 
        headers={
            'Content-Type': 'application/json',
            'X-Joyce-Schema-Id': 'joyce://schema/import/default.user'
        },
        json={
            "user_id": 1337,
            "first_name": "Jon",
            "last_name": "Snow",
            "email": "jon@winterfell.cold",
            "state": "Westeros"
        })
    
    assert response.status_code == 200
    result = response.json()
    print(result)
    assert result['processStatus'] == 'IMPORTED'

    # save schema and restart api in compose

    save_schema_to_api("test-users", "http://import-gateway:6651/api/schema/import/default/user")
    
    time.sleep(2)
    response = requests.get(f"{API}/test-users")

    assert response.status_code == 200
    result = response.json()
    print(result)

    assert len(result) == 1
    assert result[0]['code'] == 1337
    assert result[0]['full_name'] == "JON_SNOW"
