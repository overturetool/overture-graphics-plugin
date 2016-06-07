# tempo-plotting-tool

### Electron 
##### Initialize
1. `npm install`
2. `gulp init`

##### Run
1. `gulp`
2. `npm start`

### Java
##### Package
`TODO`

## Release Procedure

Version numbers: 
- even is release
- uneven are development versions which are snapshots

### Releasing the Java module

```bash
mvn -Dmaven.repo.local=repository release:clean
mvn -Dmaven.repo.local=repository release:prepare -DreleaseVersion=${RELEASE_VER} -DdevelopmentVersion=${NEW_DEV_VER} -DpushChanges=false
git push --follow-tags
mvn -Dmaven.repo.local=repository release:perform
```
