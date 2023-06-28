let create = [
  {
    "name": "API Test Collection - System Admin",
    "description": "Tests creation of collection",
    "status": "Draft",
    "author": "Newman",
    "skills": {
      "add": [
        "9d075e9d-2bde-4956-bf8c-83e5616a7c45",
        "80e70dd1-4827-45e0-8038-f745433b6d86",
        "bf408208-7e16-4a96-a769-40a9ecc161ff"
      ]
    }
  }
];

let body = {
  mode: 'raw',
  raw: JSON.stringify(create),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);