// vars/blogKubeDeploy.groovy
def call(repoName, repoOwner, dockerRegistryDomain, deploymentDomain, gcpProject = "core-workshop", Closure body) {
    def label = "kubectl"
    def podYaml = libraryResource 'podtemplates/kubectl.yml'
    def deployYaml = libraryResource 'k8s/basicDeploy.yml'
    def hostPrefix = "development"
    
    podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
      node(label) {
        body()
        repoName = repoName.toLowerCase()
        repoOwner = repoOwner.toLowerCase()
        if(env.BRANCH_NAME == "master") {
          hostPrefix = "production"
        }
        url = "http://${hostPrefix}.${repoOwner}-${repoName}.${deploymentDomain}"
        sh label: "update deployment scripts", script: """
          sed -i 's#REPLACE_IMAGE#${dockerRegistryDomain}/${repoOwner}/${repoName}:${env.VERSION}#' .kubernetes/frontend.yaml
          sed -i 's#REPLACE_HOSTNAME#${hostPrefix}.${repoOwner}-${repoName}.${deploymentDomain}#' .kubernetes/frontend.yaml
          sed -i 's#REPLACE_REPO_OWNER#${repoOwner}-${hostPrefix}#' .kubernetes/frontend.yaml
        """
        container("kubectl") {
          sh label: "${hostPrefix} deployment", script: """
            cat .kubernetes/frontend.yaml
            kubectl apply -f .kubernetes/frontend.yaml
          """
          sh label: "deployment url", script: "echo 'deployed to ${url}'"
        }
        container("jnlp") {
          gitHubCommitStatus(repoName, repoOwner, env.COMMIT_SHA, url, "your application was successfully deployed", "deployed-to-${hostPrefix}")
        }
      }
    }
}
