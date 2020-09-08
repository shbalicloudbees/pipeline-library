def call() {
  def label = "nodejs-${repoOwner}"
  def podYaml = libraryResource 'podtemplates/nodejs/pod.yml'
  def buildMode = "build"
  podTemplate(name: 'nodejs', label: label, yaml: podYaml, podRetention: always(), idleMinutes: 30) {
    body()
    if(env.BRANCH_NAME != "master") {
      buildMode = "build:dev" 
    }
    sh """
      mkdir -p app
      cd ./app
      yarn install
      yarn run $buildMode
    """
    stash name: "app", includes: "app/**" 
  }
}
