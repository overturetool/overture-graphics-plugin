/// <reference path="../../typings/globals/electron-json-storage/index.d.ts" />
/**
 * Created by John on 13-07-2016.
 */
import * as electron from "electron";
import * as FileSystem from "fs";
import * as Util from "util";
import {menuTemplate, addMacMenu} from "../menu/menuTemplate";
import * as Collections from 'typescript-collections';
import MenuItemOptions = Electron.MenuItemOptions;
import MenuItem = Electron.MenuItem;
import Menu = Electron.Menu;
import BrowserWindow = Electron.BrowserWindow;
import {Plot, PlotType} from "../model/Plot";
const Dialog = electron.remote.dialog;

export class Configuration {
    get runFunction(): string {
        return this._runFunction;
    }

    set runFunction(value: string) {
        this._runFunction = value;
    }
    get rootClass(): string {
        return this._rootClass;
    }

    set rootClass(value: string) {
        this._rootClass = value;
    }
    private _rootClass: string = "";
    private _runFunction: string = "";
    private _menu: Menu;

    plots: Collections.Dictionary<string, { variables: string[], type: PlotType }>;
    onLoad: (cfg: Configuration) => void;

    constructor() {
        // Create configuraton tab in menu
        this.plots = new Collections.Dictionary<string, { variables: string[], type: PlotType }>();
    }

    addPlot(title: string, variables: string[], type: PlotType) {
        this.plots.setValue(title, { variables, type });
    }

    save(item: MenuItem, window: BrowserWindow) {
        console.log(this.rootClass);
        if (this.rootClass !== "") {
            var json : any = {
                rootClass: this.rootClass
            };
            if(this.runFunction !== undefined) {
                json.runFunction = this.runFunction;
            }
            if(!this.plots.isEmpty()) {
                json.plots = this.plots;
            }

            Dialog.showSaveDialog({
                filters: [
                    {name: 'JSON (*.json)', extensions: ['json']},
                    {name: 'All Files', extensions: ['*']}
                ]
            }, (fileName: string) => {
                if (fileName === undefined){
                    Dialog.showMessageBox({type: "warning", title: "No filename.", buttons: ["OK"], message: "You did not provide a filename."});
                    return;
                } 
                FileSystem.writeFile(fileName, JSON.stringify(json), 'utf8', function (err) {
                    if(err){
                        Dialog.showMessageBox({type: "error", title: "Error.", buttons: ["OK"], message: "An error ocurred creating the file: "+ err.message});
                    }
                                    
                    Dialog.showMessageBox({type: "info", title: "File created.", buttons: ["OK"], message: "Configuration was successfully saved as '" + fileName + "'."});
                });
            });
        }
        else {
            Dialog.showMessageBox({type: "warning", title: "Empty configuration.", buttons: ["OK"], message: "Configuration is empty. Select a root class before saving."});
        }
    }

    load(item: MenuItem, window: BrowserWindow) {
        var self = this;
        
        Dialog.showOpenDialog({
            filters: [
                {name: 'JSON (*.json)', extensions: ['json']},
                {name: 'All Files', extensions: ['*']}
            ]
        }, (fileNames : string[]) => {
            if(fileNames === undefined) {
                console.log("No file selected");
            } else {
                var filepath = fileNames[0];
                FileSystem.readFile(filepath, 'utf8', (err, rawData) => {
                    try {
                        let data = JSON.parse(rawData);
                        
                        if(data.rootClass === undefined || data.rootClass === null || data.rootClass === "") {
                            Dialog.showMessageBox({type: "error", title: "Wrong format.", buttons: ["OK"], message: "File format error. No root class found in file."});
                            return;
                        }

                        self.rootClass = data.rootClass;
                        self.runFunction = data.runFunction;
                        self.plots.clear();
                        if(data.plots !== undefined && data.plots.table !== undefined) {
                            for (let pi in data.plots.table) {
                                let key = data.plots.table[pi].key;
                                let value = data.plots.table[pi].value;
                                self.plots.setValue(key, value);
                            }
                        }

                        if (self.onLoad !== undefined) {
                            self.onLoad(self);
                        }
                    }
                    catch(e) {
                        err = {
                            name: "FileFormatException",
                            message: "File format error."
                        };
                    }

                    if(err) {
                        Dialog.showMessageBox({type: "error", title: "Error", buttons: ["OK"], message: "An error ocurred reading the file: " + err.message});
                        return;
                    }
                });
            }
        });
    }
}