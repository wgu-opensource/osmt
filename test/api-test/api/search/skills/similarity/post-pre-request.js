let skillStatement = {
    "statement": "Validate authorization and authentication runbooks and troubleshoots procedures (SOPs), and troubleshoots advanced authentication and authorization issues."
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(skillStatement),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);