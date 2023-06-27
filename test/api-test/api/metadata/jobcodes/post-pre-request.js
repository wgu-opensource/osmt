let jobCodes = [
  {
    "name": "Manuel Category 20230619-1644",
    "url": "https://wgu.keywords/20230619-1644",
    "type": "Category",
    "framework": "20230619-1644"
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