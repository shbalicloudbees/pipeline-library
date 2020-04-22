// vars/configBundleUpdate.groovy
def call(String nameSpace = "cloudbees-core") {
  def masterName = System.properties.'MASTER_NAME'
  def label = "kubectl"
  def podYaml = libraryResource 'podtemplates/kubectl.yml'
  
  podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
    node(label) {
      checkout scm
      container("kubectl") {
        sh "mkdir ${masterName}"
        sh "cp *.yaml ${masterName}"
        sh "kubectl exec --namespace ${nameSpace} cjoc-0 -- rm -rf /var/jenkins_home/jcasc-bundles-store/${masterName}"
        sh "kubectl cp --namespace ${nameSpace} ${masterName} cjoc-0:/var/jenkins_home/jcasc-bundles-store/"
      }
    }
  }
}