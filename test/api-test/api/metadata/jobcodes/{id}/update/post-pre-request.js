let jobCode = {
  "code": "11-3013",
  "targetNodeName": "Facilities Managers",
  "frameworkName": "bls"
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(jobCode),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);