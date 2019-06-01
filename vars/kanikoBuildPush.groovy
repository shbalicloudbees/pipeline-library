// vars/dockerBuildPush.groovy
def call(String imageName, String imageTag = env.BUILD_NUMBER, String gcpProject = "core-workshop", String target = ".", String dockerFile="Dockerfile", Closure body) {
  def dockerReg = "gcr.io/${gcpProject}"
  imageName = "helloworld-nodejs"
  def label = "kaniko-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/dockerBuildPush.yml'
  podTemplate(name: 'kaniko', label: label, yaml: podYaml, inheritFrom: 'default-jnlp') {
    node(label) {
      imageNameTag()
      gitShortCommit()
      def repoName = env.IMAGE_REPO.toLowerCase()
      container(name: 'kaniko', shell: '/busybox/sh') {
        body()
        withEnv(['PATH+EXTRA=/busybox:/kaniko']) {
          sh """#!/busybox/sh
            /kaniko/executor -f ${pwd()}/${dockerFile} -c ${pwd()} --build-arg context=${repoName} --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${SHORT_COMMIT} --build-arg commitAuthor=${COMMIT_AUTHOR} -d ${dockerReg}/${repoName}:${BUILD_NUMBER}
          """
        }
        }
      }
    }
}
