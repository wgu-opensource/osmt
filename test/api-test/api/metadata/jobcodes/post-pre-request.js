let jobCodes = [
  {
    "code": "95-1240",
    "targetNodeName": "20230622-1519",
    "frameworkName": "Framework 20230622-1519"
  }
];

let body = {
  mode: 'raw',
  raw: JSON.stringify(jobCodes),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);