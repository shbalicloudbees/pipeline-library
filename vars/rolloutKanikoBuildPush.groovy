// vars/kanikoBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", String target = ".", String dockerFile="Dockerfile", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  //imageName = "helloworld-nodejs"
  def label = "kaniko-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/dockerBuildPush.yml'
  def folderName = ""
  podTemplate(name: 'kaniko', label: label, yaml: podYaml, inheritFrom: 'default-jnlp', nodeSelector: 'type=agent') {
    node(label) {
      if(imageName=="rollout-js"){
        folderName = "frontend-spring-boot-react-crud-full-stack-with-maven"
      }
      else if(imageName=="rollout-java"){
        folderName = "backend-spring-boot-react-crud-full-stack-with-maven"
      }
      body()
      rolloutImageNameTag()
      gitShortCommit()
      def repoName = env.IMAGE_REPO.toLowerCase()
      container(name: 'kaniko', shell: '/busybox/sh') {
        withEnv(['PATH+EXTRA=/busybox:/kaniko']) {
          echo "Dockerfile path: ${pwd()}/${folderName}/${dockerFile}"
          sh """#!/busybox/sh
            /kaniko/executor -f ${pwd()}/${folderName}/${dockerFile} -c ${pwd()} --build-arg context=${repoName} --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${env.SHORT_COMMIT} --build-arg commitAuthor=${env.COMMIT_AUTHOR} -d ${dockerReg}/${imageName}:${repoName}-${BUILD_NUMBER}
          """
        }
      }
      publishEvent event:jsonEvent("{'eventType':'containerImagePush', 'image':'${dockerReg}/${imageName}:${repoName}-${BUILD_NUMBER}'}"), verbose: true
    }
  }
}
