def call(String gitHubOrg, String credentialId = 'cloudbees-ci-workshop-github-app') {
  if(env.BUILD_NUMBER.equals("1")) {
    withCredentials([usernamePassword(credentialsId: "${credentialId}", usernameVariable: 'GITHUB_APP', passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
      sh """
        curl -H 'Accept: application/vnd.github.antiope-preview+json' \
           -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
           -X PATCH \
           https://api.github.com/repos/${gitHubOrg}/cloudbees-ci-config-bundle/pulls/4 \
           --data '{"state":"closed"}
      """
    }
  }
}
