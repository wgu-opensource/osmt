let skill = [
  {
      "skillName": "API Test RSD - Master Basketweaver",
      "skillStatement": "Most proficient in the weaving of baskets.",
      "category": "Home Care",
      "occupations": {
          "add": [
              "11-1011"
          ],
          "remove": []
      },
      "author": "Western Governors University"
  }
];

let body = {
  mode: 'raw',
  raw: JSON.stringify(skill),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);
