import http from 'k6/http';
import encoding from 'k6/encoding';

import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';

const encodedCredentials = encoding.b64encode("admin:password");
const fd = new FormData();

// k6 can't write files to fs, so they must be provided from the outside
let files = [/*LIST OF PACKAGES NAME*/]
files = files.split(",")

const BASE_URL = __ENV.MY_HOSTNAME;
const fileMap = files.reduce((current, file) =>{
    current[file] = open(`${__ENV.PWD}/packages/${file}`, "b");
    return current;
}, {});

export default function () {
    const repoName = createLocalRepoByVuNumber();
    uploadFilesToArtifactory(repoName);
}

function createLocalRepoByVuNumber() {
    const repoConfig = getLocalRepoConfig();
    const headers = {
        headers: {
            Authorization: `Basic ${encodedCredentials}`,
            'Content-Type': 'application/json'
        }
    };
    
    const response = http.put(`${BASE_URL}/artifactory/api/repositories/${repoConfig.key}`, JSON.stringify(repoConfig), headers);
    console.log(`Creating repo ${repoConfig.key} returned status ${response.status}`);
    return repoConfig.key;
}

function getLocalRepoConfig() {
    console.log(`about to create ${__VU}`);
    return {
        "key": `local-gems-${__VU}`,
         "rclass" : "local",
        "packageType": "gems",
        "description": "The local repository public description",
       }
}

function uploadFilesToArtifactory(repoName) {
    let i=0;
    const headers = {
        headers: {
            'Content-Type': 'multipart/form-data; boundary=' + fd.boundary,
            Authorization: `Basic ${encodedCredentials}`
        }
    };
    for (const [fileName, content] of Object.entries(fileMap)) {
        const blob = http.file(content, fileName)
        const response = http.put(`${BASE_URL}/artifactory/${repoName}/gems/${fileName}`, blob.data, headers);
        if (+response.status > 299){
        console.log(response.status, i++);}
    }
}
