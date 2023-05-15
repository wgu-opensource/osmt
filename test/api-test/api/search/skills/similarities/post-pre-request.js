let skillStatements = [
  {
      "statement": "Validate authorization and authentication runbooks and troubleshoots procedures (SOPs), and troubleshoots advanced authentication and authorization issues."
  },
  {
      "statement": "Create group and system authentication access."
  },
  {
      "statement": "Design network connections between a core network and an internet service provider."
  },
  {
      "statement": "Monitor network routers for performance issues."
  },
  {
      "statement": "Monitor network routers for security issues."
  },
  {
      "statement": "Configure network routers for traffic between a core network and an internet service provider."
  },
  {
      "statement": "Implement schema extensions for Active Directory."
  },
  {
      "statement": "Configure forest and domain trusts for Active Directory."
  },
  {
      "statement": "Manage project feature progress with agile Kanban boards."
  },
  {
      "statement": "Manage project feature progress with agile scrum boards."
  },
  {
      "statement": "Communicate blocking issues during the development cycle."
  },
  {
      "statement": "Accomplish agile project goals through collaboration with internal partners."
  },
  {
      "statement": "Deploy a cloud computing environment in Amazon Web Services (AWS)."
  },
  {
      "statement": "Maintain a cloud computing environment in Amazon Web Services (AWS)."
  },
  {
      "statement": "Access an application programming interface (API) with a programming language to change data for a task."
  },
  {
      "statement": "Engineer systems that leverage multi-platform application programming interface (API)."
  },
  {
      "statement": "Configure file backups to a cloud solution."
  },
  {
      "statement": "Identify which backed-up files must be restored."
  },
  {
      "statement": "Identify disaster recovery solutions for backing up and restoring  computer systems."
  },
  {
      "statement": "Determine the hardware needed for the backup of a device's data."
  },
  {
      "statement": "Determine which files must be backed up from one device to another."
  },
  {
      "statement": "Perform manual backups of data from a device."
  },
  {
      "statement": "Implement Border Gateway Protocol solutions."
  },
  {
      "statement": "Implement continuity plans during the time of a disaster."
  }
];

let body = {
  mode: 'raw',
  raw: JSON.stringify(skillStatements),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);