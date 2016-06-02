'use strict';
///<reference path="../typings/globals/github-electron/index.d.ts"/>
///<reference path="../typings/globals/node/index.d.ts"/>
const fs = require('fs');
const Path = require('path');

export default class App {
    app: Electron.App;
    platform : String
    window: Electron.BrowserWindow;

    constructor(app: Electron.App, processPlatform: String) {
        this.app = app;
        this.platform = processPlatform;

        const AppFolder = this.createAppFolderRoot(app);
        this.createDirectoryStructure(AppFolder);
    }

    public setWindow(win: Electron.BrowserWindow) {
        this.window = win;
    }


    private createAppFolderRoot(app: Electron.App): string {
        const path = require('path');
        // Create App folder
        const userPath = function () {
            if (app.getPath("exe").indexOf("electron-prebuilt") > -1) {

                console.log("Dev-mode: Using " + __dirname + " as user data path.")
                return __dirname;
            }
            else {
                return app.getPath('userData');
            }
        } ();

        return path.normalize(userPath + "/App");
    }

    private createDirectoryStructure(path: string) {
        try {
            fs.mkdirSync(path);
        } catch (e) {
            //the path probably already existed
        }
    }

    // Fires an ipc event using the window webContent if defined
    private fireEvent(event: string) {
        if (this.window != undefined) {
            //Fire an event to inform all controlls on main window that the project has changed
            // console.info("Window: " + this.window);
            console.info("fire event: " + event);
        }
    }

}
