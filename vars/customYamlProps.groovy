// vars/customYamlProps.groovy
def call(String file = 'cloudbees-ci.yml') {
  //defaults from job name for Docker image
  imageNameTag()
  //set SHORT_COMMIT for all defineProps calls
  gitShortCommit()
  //use the Pipeline Utility Steps plugin readYaml step to read in specified custom marker file 
  def props = readYaml file: file
  for ( e in props ) {
    env.setProperty(e.key, e.value)
  }
}
