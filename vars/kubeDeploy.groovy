// vars/kubeDeploy.groovy
def call(imageName, imageTag) {
    def label = "kubectl"
    def podYaml = libraryResource 'podtemplates/kubeDeploy.yml'
    def deployYaml = libraryResource 'k8s/basicDeploy.yml'
    def repoName = env.IMAGE_REPO.toLowerCase()
    podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
      node(label) {
        writeFile file: "deploy.yml", text: deployYaml
        container("kubectl") {
          sh("sed -i.bak 's#REPLACE_IMAGE_TAG#gcr.io/core-workshop/${repoName}-${imageName}:${BUILD_NUMBER}#' deploy.yml")
          sh("sed -i.bak 's#REPLACE_SERVICE_NAME#${repoName}#' deploy.yml")
          sh "kubectl apply -f deploy.yml"
          sh "echo 'deployed to http://prod.cb-sa.io/${repoName}/'"
        }
      }
    }
}
