// vars/kubeDeploy.groovy
def call(repoName, dockerRegistryDomain, deploymentDomain, repoOwner, gcpProject = "core-workshop") {
    def label = "kubectl"
    def podYaml = libraryResource 'podtemplates/kubeDeploy.yml'
    def deployYaml = libraryResource 'k8s/basicDeploy.yml'
    
    podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
      node(label) {
        imageNameTag()
        def imageName = repoName.toLowerCase()
        checkout scm
        sh("sed -i.bak 's#REPLACE_IMAGE#${dockerRegistryDomain}/${imageName}:${env.VERSION}#' .kubernetes/frontend.yaml")
        sh("sed -i.bak 's#REPLACE_HOSTNAME#staging.${repoOwner}.${deploymentDomain}#' .kubernetes/frontend.yaml")
        sh("sed -i.bak 's#REPLACE_PATH#/${repoOwner}#' .kubernetes/frontend.yaml")
        container("kubectl") {
          sh "cat .kubernetes/frontend.yaml"
          sh "kubectl apply -f .kubernetes/frontend.yaml"
          sh "echo 'deployed to http://staging.${repoOwner}.${deploymentDomain}'"
        }
      }
    }
}
