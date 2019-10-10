// vars/kubeDeploy.groovy
def call(imageName, imageTag, githubCredentialId, repoOwner) {
    def label = "kubectl"
    def podYaml = libraryResource 'podtemplates/kubeDeploy.yml'
    def deployYaml = libraryResource 'k8s/basicDeploy.yml'
    def repoName = env.IMAGE_REPO.toLowerCase()
    def envStagingRepo = "environment_staging"
    def pullMaster = true
    
    podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
      node(label) {
        //create environment repo for prod if it doesn't already exist
        echo githubCredentialId
        withCredentials([usernamePassword(credentialsId: githubCredentialId, usernameVariable: 'USERNAME', passwordVariable: 'ACCESS_TOKEN')]) {
          echo repoOwner
          echo envStagingRepo

        int status = sh(script: """
            curl -w '%{http_code}' -H "Authorization: token $ACCESS_TOKEN" --data '{"name":"${envStagingRepo}"}' https://api.github.com/orgs/${repoOwner}/repos
          """, returnStdout: true)
        }
        echo "repo create returned status: ${status}"
        if(status!=200){
          pullMaster=true
        }
        withCredentials([usernamePassword(credentialsId: githubCredentialId, usernameVariable: 'USERNAME', passwordVariable: 'ACCESS_TOKEN')]) {
          sh """
            git init
            git config user.email "deployBot@cb-sa.io"
            git config user.name "${USERNAME}"
            git remote add origin https://${USERNAME}:${ACCESS_TOKEN}@github.com/${repoOwner}/${envStagingRepo}.git
          """

          echo "pullMaster: ${pullMaster}"
          if(pullMaster) {
            sh 'git pull origin master'
          } else {
            writeFile file: "deploy.yml", text: deployYaml
            sh 'git add deploy.yml'
          }

          sh("sed -i 's#REPLACE_IMAGE_TAG#gcr.io/core-workshop/helloworld-nodejs:${repoName}-${BUILD_NUMBER}#' deploy.yml")
          sh("sed -i 's#REPLACE_SERVICE_NAME#${repoName}#' deploy.yml")
          sh """
            git add *
            git commit -a -m 'updating ${envStagingRepo} deployment for ${repoName}'
            git push -u origin master
          """
        }
        container("kubectl") {
          sh "kubectl apply -f deploy.yml"
          sh "echo 'deployed to http://staging.cb-sa.io/${repoName}/'"
        }
      }
    }
}
