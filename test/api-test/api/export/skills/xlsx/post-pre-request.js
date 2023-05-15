let skills = {
  "uuids": [
      "91820bcb-85cc-4d11-ae75-b43d2a3619af",
      "50b56c4f-839d-4d66-b2bc-65169cca1ca9",
      "7379e1fb-b9c9-43ec-a199-80245145ab11",
      "d1e1b49e-e3fa-405e-9bd7-e29818c0f8a0"
  ]
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(skills),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);