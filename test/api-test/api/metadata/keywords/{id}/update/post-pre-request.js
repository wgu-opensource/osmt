let updates = {
  "framework": "framework1",
  "name": "Virtual Computing",
  "type": "Alignment",
  "url": "https://skills.emsidata.com/skills/KS441TD6115P4M4G1LFZ"
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