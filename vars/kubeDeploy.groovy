// vars/kubeDeploy.groovy
def call(imageName, imageTag, githubCredentialId, repoOwner) {
    def label = "kubectl"
    def podYaml = libraryResource 'podtemplates/kubeDeploy.yml'
    def deployYaml = libraryResource 'k8s/basicDeploy.yml'
    def repoName = env.IMAGE_REPO.toLowerCase()
    def envProdRepo = "environment_prod"
    
    podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
      node(label) {
        //create environment repo for prod if it doesn't already exist
        echo githubCredentialId
        withCredentials([usernamePassword(credentialsId: githubCredentialId, usernameVariable: 'USERNAME', passwordVariable: 'ACCESS_TOKEN')]) {
          echo repoOwner
          echo envProdRepo
          def repoNotExists = sh(script: '''
              curl -H "Authorization: token $ACCESS_TOKEN" https://api.github.com/repos/$repoOwner/$envProdRepo | jq 'contains({message: "Not Found"})'
            ''', returnStdout: true)
          echo repoNotExists
          if(repoNotExists) {
          sh(script: '''
              curl -H "Authorization: token $ACCESS_TOKEN" --data '{"name":$envProdRepo}' https://api.github.com/orgs/$repoOwner/repos
            ''')
          }
          //curl -H "Authorization: token ACCESS_TOKEN" --data '{"name":""}' https://api.github.com/orgs/ORGANISATION_NAME/repos
        }
        writeFile file: "deploy.yml", text: deployYaml
        sh("sed -i.bak 's#REPLACE_IMAGE_TAG#gcr.io/core-workshop/${repoName}:${BUILD_NUMBER}#' deploy.yml")
        sh("sed -i.bak 's#REPLACE_SERVICE_NAME#${repoName}#' deploy.yml")
        sh '''
          git config user.email "deployBot@cb-sa.io"
          git config user.name "Deploy Bot"
          git init
          git add deploy.yml
          git commit -a -m "updating $envProdRepo deployment for $repoName"
          git remote add origin https://github.com/bee-cd/$envProdRepo.git
          git push -u origin master
        '''
        container("kubectl") {
          sh "kubectl apply -f deploy.yml"
          sh "echo 'deployed to http://prod.cb-sa.io/${repoName}/'"
        }
      }
    }
}
