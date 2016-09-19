


compile: prepare
	gulp build



package: compile
#	gulp package-darwin
	gulp package-all
#	cd pkg; zip -r overture-graphics-plugin-darwin-x64.zip overture-graphics-plugin-darwin-x64

compress:
	cd pkg; for i in */; do zip -r "${i%/}.zip" "$i"; done

prepare:
	npm install --no-progress
	npm list
	gulp init


clean:
	rm -rf node_modules
