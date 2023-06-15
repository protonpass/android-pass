#!/bin/bash

set -euxo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts/publish::g')

# Install required tools
apt update && apt install -y git-filter-repo connect-proxy

# Add key to ssh-agent
eval $(ssh-agent -s)
echo "${GITHUB_PUBLISH_SSH_KEY}" | base64 -d | tr -d '\r' | ssh-add - > /dev/null
mkdir ~/.ssh 2> /dev/null || true

# Set up ssh config
cat <<EOF > ~/.ssh/config
Host github.com
Hostname ssh.github.com
User git
Port 443
ProxyCommand connect-proxy -H $http_proxy %h %p
EOF

ssh-keyscan -t rsa "$CI_SERVER_HOST" > ~/.ssh/known_hosts

# ssh-keyscan doesn't support proxies so we can't fetch the fingerprint online
cat <<EOF >> ~/.ssh/known_hosts
[ssh.github.com]:443 ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCj7ndNxQowgcQnjshcLrqPEiiphnt+VTTvDP6mHBL9j1aNUkY4Ue1gvwnGLVlOhGeYrnZaMgRK6+PKCUXaDbC7qtbW8gIkhL7aGCsOr/C56SJMy/BCZfxd1nWzAOxSDPgVsmerOBYfNqltV9/hWCqBywINIR+5dIg6JTJ72pcEpEjcYgXkE2YEFXV1JHnsKgbLWNlhScqb2UmyRkQyytRLtL+38TGxkxCflmO+5Z8CSSNY7GidjMIZ7Q4zMjA2n1nGrlTDkzwDCsw+wqFPGQA179cnfGWOWRVruj16z6XyvxvjJwbz0wQZ75XK5tKSb7FNyeIEs4TT4jk+S4dhPeAUC5y+bDYirYgM4GC7uEnztnZyaVWQ7B381AK4Qdrwt51ZqExKbQpTUNn+EjqoTwvqNj4kqx5QUCI0ThS/YkOxJCXmPUWZbhjpCg56i+2aB6CmK2JGhn57K5mj0MNdBXA4/WnwH6XoPWJzK5Nyu2zB3nAZp+S5hpQs+p1vN1/wsjk=
EOF

# SSH is set up. Proceed to publish
$REPO_ROOT/scripts/publish/publish.sh /tmp/app-clone "${GITHUB_PUBLIC_URL}"
