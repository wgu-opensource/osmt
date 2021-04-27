const {readdirSync, readFileSync, writeFileSync, mkdirSync} = require("fs");
let allFiles = [];
const kubeDir = `${__dirname}/kube`
const tmpDir = `${kubeDir}/tmp`;
const dontInclude = ["nginx/nginx-elb.yml", "nginx"];

const createSubDomains = () => {
    const fileNames = readdirSync(kubeDir);

    fileNames.forEach( (file) =>{
        if(file.match(/\./g) === null){
            let innerFiles = readdirSync(`${kubeDir}/${file}`);
            innerFiles = innerFiles.map( el => `${file}/${el}`)
            allFiles = [...allFiles, ...innerFiles];
        }
        allFiles.push(file);
    })

    allFiles = allFiles.filter( file => !dontInclude.includes(file));
    mkdirSync(tmpDir);
    allFiles.forEach( (filePath) => {
        let fileDump = readFileSync(`${kubeDir}/${filePath}`).toString();
        if(filePath.includes('/')){
            filePath = filePath.split("/")[1]
        }
        fileDump = fileDump.replace(/\{\{customerId\}\}/g, process.argv[2]);
        writeFileSync(`${tmpDir}/${filePath}`, fileDump);
    })
}

createSubDomains()
