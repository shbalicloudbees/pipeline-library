// vars/imgBuildNexus.groovy
def call(String repoName, String repoOwner, String registry, Closure body) {
  def label = "img-${UUID.randomUUID().toString()}"
  def podYaml = libraryResource 'podtemplates/imageBuildPushNexus.yml'
  def imageName = repoName.toLowerCase()
  podTemplate(name: 'img', label: label, yaml: podYaml) {
    node(label) {
      body()
      script {
        env.VERSION = readFile 'version.txt'
      }
      imageNameTag()
      gitShortCommit()
      container('img') {
        sh """
          img build --build-arg buildNumber=${BUILD_NUMBER} --build-arg shortCommit=${env.SHORT_COMMIT} --build-arg commitAuthor="${env.COMMIT_AUTHOR}" -t ${registry}/${repoOwner}/${imageName}:${env.VERSION.trim()} ${pwd()}
          img push ${registry}/${repoOwner}/${imageName}:${env.VERSION}
        """
      }
    }
  }
}
