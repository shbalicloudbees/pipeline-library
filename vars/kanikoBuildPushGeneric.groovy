// vars/kanikoBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", String target = ".", String dockerFile="Dockerfile", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  def label = "img-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/dockerBuildPush.yml'
  podTemplate(name: 'img', label: label, yaml: podYaml, nodeSelector: 'type=agent') {
    node(label) {
      body()
      imageNameTag()
      gitShortCommit()
      container('gcp-sdk') {
        sh "gcloud auth configure-docker"
      }
      container('img') {
        sh """
          img build -t ${dockerReg}/${imageName}:${imageTag} .  
        """
      }
    }
  }
}
