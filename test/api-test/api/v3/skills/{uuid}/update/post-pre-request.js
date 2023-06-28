let payload = {
  authors: {
    add: [
      "Bilbo Baggins"
    ]
  },
  categories: {
    add: [
      "Walls of Fire"
    ]
  }
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(payload),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);