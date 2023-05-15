let skill = [
  {
      "skillName": "API Test RSD - Master Basketweaver",
      "skillStatement": "Most proficient in the weaving of baskets.",
      "categories": {
          "add": [
              "Home Care"
          ],
          "remove": []
      },
      "occupations": {
          "add": [
              "11-1011"
          ],
          "remove": []
      },
      "authors": {
          "add": [
              "Western Governors University"
          ],
          "remove": []
      }
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