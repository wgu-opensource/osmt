let create =
    {
      "framework": "framework1",
      "name": "Scrum Methodology",
      "type": "Category",
      "url": "url1"
    }
;

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