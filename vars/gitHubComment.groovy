def call(Map config) {        
  withCredentials([usernamePassword(credentialsId: "${config.credId}", usernameVariable: 'GITHUB_APP', passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
    sh """
      curl -s -H "Authorization: Bearer ${GITHUB_ACCESS_TOKEN}" \
        -X POST -d '{"body": "${config.message}"}' \
        "https://api.github.com/repos/${config.repoOwner}/${config.repo}/issues/${config.issueId}/comments"
    """
  }
}        
