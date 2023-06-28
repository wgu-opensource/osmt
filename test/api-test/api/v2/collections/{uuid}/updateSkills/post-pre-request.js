let updates = {
  "add": {
    "uuids": [
      "9d075e9d-2bde-4956-bf8c-83e5616a7c45",
      "95ecf6db-69f6-40f6-8ac9-f5f2260c52e6"
    ]
  },
  "remove": {
    "uuids": [
      "aabb1eee-48b7-4d46-b4e0-fb584285627d"
    ]
  }
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(updates),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);