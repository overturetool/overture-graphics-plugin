// ITC Marking: UTC Proprietary - Export Controlled - Created at UTRC-I, ECCN NLR
// Copyright UTRC 2016

'use strict';

// Locations 
var outputPath = 'dist/',
    htmlSrcs = ['src/**/*.html'],
    jsSrcs = 'src/**/*.js',
    lintTsSrcs = ['src/**/*.ts'],
    tsSrcs = ['src/**/*.ts', 'typings/browser/**/*.ts'],
    bowerFolder = 'bower_components',
    resourcesFolder = 'src/resources',
    typingsFolder = 'typings',
    cssSrcs = [bowerFolder + '/bootstrap/dist/css/bootstrap.css',
        resourcesFolder + '/w2ui-1.5/w2ui.min.css'],
    bowerSrcs = "";

// Tools.
var gulp = require('gulp'),
    ts = require('gulp-typescript'),
    sourcemap = require('gulp-sourcemaps'),
    tsProject = ts.createProject('tsconfig.json'),
    lint = require('gulp-tslint'),
    del = require('del'),
    mainBowerFiles = require('main-bower-files'),
    filter = require('gulp-filter'),
    debug = require('gulp-debug'),
    typings = require('gulp-typings'),
    bower = require('gulp-bower'),
    merge = require('merge-stream'),
    electron = require('gulp-electron'),
    packager = require('electron-packager'),
    packageJSON = require('./package.json');

// Tasks

// Install typings
gulp.task("install-ts-defs", function () {
    gulp.src("./typings.json")
        .pipe(typings());
});

// Install bower components
gulp.task('install-bower-components', function () {
    return bower();
});

// Clean everything!
gulp.task("clean", function () {
    return del([
        outputPath
    ]);
});

// Lint TS (check for rule violations)
gulp.task("lint-ts", function () {
    return gulp.src(lintTsSrcs).pipe(lint()).pipe(lint.report('prose', { emitError: false }));
});

// Compile TS->JS with sourcemaps. Also move it into the outputfolder
gulp.task("compile-ts", function () {
    var tsResult = gulp.src(tsSrcs)
        .pipe(sourcemap.init())
        .pipe(ts(tsProject));

    tsResult.dts.pipe(gulp.dest(outputPath));

    return tsResult.js.pipe(sourcemap.write('.'))
        .pipe(gulp.dest(outputPath));
});

// Copy important bower files to destination
// mainBowerFiles does not take jquery-ui and jquery-layout
gulp.task('copy-bower', function () {
    var path1 = gulp.src(mainBowerFiles())
        .pipe(filter('**/*.js'))
        .pipe(gulp.dest(outputPath + bowerFolder));
    var path2 = gulp.src(bowerSrcs).pipe(gulp.dest(outputPath + bowerFolder));
    return merge(path1, path2);
});

// Copy bootstrap fonts to destination
gulp.task('copy-fonts', function () {
    return gulp.src(bowerFolder + '/bootstrap/fonts/**/*').pipe(gulp.dest(outputPath + 'fonts'))
});


// Copy css to app folder
gulp.task('copy-css', function () {
    gulp.src(cssSrcs).pipe(gulp.dest(outputPath + 'css'));
});

// Copy html to app folder
gulp.task('copy-html', function () {
    gulp.src(htmlSrcs)
        // process html here if needed
        .pipe(gulp.dest(outputPath));
});

// Copy js to app folder
gulp.task('copy-js', function () {
    gulp.src(jsSrcs)
        // process js here if needed
        .pipe(gulp.dest(outputPath));
});

// Grab non-npm dependencies
gulp.task('init', ['install-ts-defs', 'install-bower-components']);

//Build App
gulp.task('build', ['compile-ts', 'copy-js', 'copy-html', 'copy-css', 'copy-bower', 'copy-fonts']);


// Package app binaries
gulp.task("package-darwin", function (callback) {
    var options = {
        dir: '.',
        name: packageJSON.name,
        platform: "darwin",
        arch: "x64",
        icon: "Overture-logo.icns",
        version: "0.36.0",
        overwrite: true,
        out: 'pkg',
        "app-version": packageJSON.version,
        "version-string": {
            "CompanyName": packageJSON.author.name,
            "ProductName": packageJSON.productName
        }
    };
    packager(options, function done(err, appPath) {
        if (err) { return console.log(err); }
        callback();
    });
});

gulp.task("package-win32", function (callback) {
    var options = {
        dir: '.',
        name: packageJSON.name,
        platform: "win32",
        arch: "all",
        icon: "Overture-logo.ico",
        version: "0.36.0",
        overwrite: true,
        out: 'pkg',
        "app-version": packageJSON.version,
        "version-string": {
            "CompanyName": packageJSON.author.name,
            "ProductName": packageJSON.productName
        }
    };
    packager(options, function done(err, appPath) {
        if (err) { return console.log(err); }
        callback();
    });
});

gulp.task("package-linux", function (callback) {
    var options = {
        dir: '.',
        name: packageJSON.name,
        platform: "linux",
        arch: "x64",
        icon: "Overture-logo.ico",
        version: "0.36.0",
        overwrite: true,
        out: 'pkg',
        "app-version": packageJSON.version,
        "version-string": {
            "CompanyName": packageJSON.author.name,
            "ProductName": packageJSON.productName
        }
    };
    packager(options, function done(err, appPath) {
        if (err) { return console.log(err); }
        callback();
    });
});

gulp.task('package-all', ['package-win32', 'package-darwin', 'package-linux']);

// Watch for changes and rebuild on the fly
gulp.task('watch', function () {
    gulp.watch(htmlSrcs, ['copy-html']);
    gulp.watch(jsSrcs, ['copy-js']);
    gulp.watch(tsSrcs, ['compile-ts']);
});


// Default task 
gulp.task('default', ['build']);
