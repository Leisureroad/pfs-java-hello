# 启动本地的docker registry和kubernetes
```docker run -d -p 5000:5000 --restart=always --volume ~/.registry/storage:/var/lib/registry registry:2```

# 设置环境变量
```
export REGISTRY=registry.pfs.svc.cluster.local:5000
export REGISTRY_USER=testuser
```

# Relocate image
```
pfs image relocate \
  --output pfs-relocated \
  --manifest pfs-download/manifest.yaml \
  --images pfs-download/image-manifest.yaml \
  --registry $REGISTRY \
  --registry-user $REGISTRY_USER
  ```

# Push image
```pfs image push --images pfs-relocated/image-manifest.yaml```

# 创建namespace
```
kubectl create namespace pfs
kubectl create service externalname registry -n pfs --external-name=host.docker.internal --tcp=5000:5000
```

# 安装PFS
```pfs system install -m pfs-relocated/manifest.yaml --node-port```

# 初始化namespace
```pfs namespace init default -m pfs-relocated/manifest.yaml --no-secret```

```
export PFS_BUILDER_IMAGE=`grep -o "$REGISTRY/$REGISTRY_USER/projectriff-builder.*" \
  pfs-relocated/image-manifest.yaml | awk -F": " '{print $1}'`

export PFS_PACKS_RUN_IMAGE=`grep -o "$REGISTRY/$REGISTRY_USER/packs-run.*" \
  pfs-relocated/image-manifest.yaml | awk -F": " '{print $1}'`
```

# 对比一下没有pfs这样的平台，是如何进行容器式应用的？
https://github.com/jldec/hello-node/tree/07e99390647a9f33352fbb2e781cc16ae184397a

# 创建函数
## nodejs-本地构建
```
pfs function create square \
  --local-path . \
  --artifact square.js \
  --image registry.pfs.svc.cluster.local:5000/testuser/square \
  --verbose
```

## java-github
```
pfs function create uppercase \
  --git-repo https://github.com/projectriff-samples/java-boot-uppercase.git \
  --image $REGISTRY/$REGISTRY_USER/uppercase \
  --verbose
```

# Invoke function - uppercase
```pfs service invoke uppercase --text -- -w '\n' -d 'welcome to pfs'```

# Create a js funciton
```
pfs function create hello \
--git-repo https://github.com/projectriff-samples/hello.js \
--artifact hello.js \
--image $REGISTRY/$REGISTRY_USER/hello \
--verbose

pfs function create hello \
--git-repo https://github.com/jldec/java-hello \
--handler functions.Hello \
--image $REGISTRY/$REGISTRY_USER/hello \
--verbose

pfs function create hello \
--git-repo https://github.com/Leisureroad/pfs-java-hello \
--handler HelloFunction \
--image $REGISTRY/$REGISTRY_USER/hello \
--verbose
```
# Invoke hello
```pfs service invoke hello --text -- -w '\n' -d 'PFS'```

# Create square function
```
pfs function create square \
  --git-repo https://github.com/projectriff-samples/node-square \
  --artifact square.js \
  --image $REGISTRY/$REGISTRY_USER/square \
  --verbose

pfs service invoke square --text -- -d 8
```
# Create random funciton
```
pfs service create random --image jldec/random:v0.0.2

pfs service invoke random --json -- -w '\n' -d '{"url":"http://hello.default.svc.cluster.local"}'
```
# Create channel
```pfs channel create numbers --cluster-bus stub```
```pfs channel create squares --cluster-bus stub```

# Create subscription
```pfs subscription create --channel numbers --subscriber square --reply-to squares```
```pfs subscription create --channel squares --subscriber hello```

```pfs service invoke random --json -- -w '\n' -d '{"url":"http://numbers-channel.default.svc.cluster.local"}'```

# Kill activator
```kubectl delete pod -l app=activator -n knative-serving```




