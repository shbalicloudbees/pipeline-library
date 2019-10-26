// vars/kanikoBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", String target = ".", String dockerFile="Dockerfile", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  def label = "img-gcloud-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/dockerBuildPush.yml'
  podTemplate(name: 'img-gcloud', label: label, yaml: podYaml, nodeSelector: 'type=master') {
    node(label) {
      body()
      imageNameTag()
      gitShortCommit()
      container('img-gcloud') {
        sh """
          img build --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${env.SHORT_COMMIT} --build-arg commitAuthor="${env.COMMIT_AUTHOR}" -t ${dockerReg}/${imageName}:${imageTag} ${pwd()}
          echo /home/user/google-cloud-sdk/bin/gcloud auth print-access-token
          /home/user/google-cloud-sdk/bin/gcloud auth print-access-token | img login -u oauth2accesstoken --password-stdin https://gcr.io
          img push ${dockerReg}/${imageName}:${imageTag}
        """
      }
    }
  }
}
