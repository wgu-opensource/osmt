let updates = {
  "description": "Computer Networking in Information Technology"
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