// vars/containerBuildPushGeneric.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  def label = "img-${repoOwner}"
  def podYaml = libraryResource 'podtemplates/containerBuildPush.yml'
  def customBuildArg = ""
  def buildModeArg = ""
  podTemplate(name: 'img', label: label, yaml: podYaml) {
    node(label) {
      body()
      try {
        env.VERSION = readFile 'version.txt'
        env.VERSION = env.VERSION.trim()
        env.VERSION = "${env.VERSION}-${BUILD_NUMBER}"
        imageTag = env.VERSION
      } catch(e) {}
      if(env.EVENT_PUSH_IMAGE_TAG) {
        customBuildArg = "--build-arg NODE_IMAGE=${env.EVENT_PUSH_IMAGE_NAME}:${env.EVENT_PUSH_IMAGE_TAG}"
      }
      if(env.BRANCH_NAME != "master") {
        buildModeArg = "--build-arg BUILD_MODE=build:dev" 
      }
      imageName = imageName.toLowerCase()
      container('gcp-sdk') {
        try {
          sh "printenv"
          sh "echo $prevImageTag"
          sh "gcloud container images delete ${dockerReg}/${imageName}:${prevImageTag}  --force-delete-tags --quiet"
        } catch(e) {}
      }
      container('img') {
        sh """
          img build ${buildModeArg} --build-arg buildNumber=${BUILD_NUMBER} ${customBuildArg} ${customBuildArg} --build-arg shortCommit=${env.SHORT_COMMIT} --build-arg commitAuthor="${env.COMMIT_AUTHOR}" -t ${dockerReg}/${imageName}:${imageTag} ${pwd()}
          cat /home/user/key/gcr-key.json | img login -u _json_key --password-stdin https://gcr.io
          img push ${dockerReg}/${imageName}:${imageTag}     
        """
        env.prevImageTag=${imageTag}
      }
    }
  }
}
