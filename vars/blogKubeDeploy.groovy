// vars/blogKubeDeploy.groovy
def call(repoName, repoOwner, dockerRegistryDomain, deploymentDomain, gcpProject = "core-workshop", Closure body) {
    def label = "kubectl"
    def podYaml = libraryResource 'podtemplates/kubectl.yml'
    def deployYaml = libraryResource 'k8s/basicDeploy.yml'
    
    podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
      node(label) {
        body()
        repoName = repoName.toLowerCase()
        repoOwner = repoOwner.toLowerCase()
        sh("sed -i 's#REPLACE_IMAGE#${dockerRegistryDomain}/${repoOwner}/${repoName}:${env.VERSION}#' .kubernetes/frontend.yaml")
        sh("sed -i 's#REPLACE_HOSTNAME#staging.${repoOwner}-${repoName}.${deploymentDomain}#' .kubernetes/frontend.yaml")
        sh("sed -i 's#REPLACE_REPO_OWNER#${repoOwner}#' .kubernetes/frontend.yaml")
        container("kubectl") {
          sh "cat .kubernetes/frontend.yaml"
          sh "kubectl apply -f .kubernetes/frontend.yaml"
          sh "echo 'deployed to http://staging.${repoOwner}-${repoName}.${deploymentDomain}'"
        }
      }
    }
}
