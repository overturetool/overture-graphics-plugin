/// <reference path="../../typings/globals/electron-json-storage/index.d.ts" />
/**
 * Created by John on 13-07-2016.
 */
import * as electron from "electron";
import {menuTemplate} from "../menu/menuTemplate";
import * as Collections from 'typescript-collections';
import MenuItemOptions = Electron.MenuItemOptions;
import MenuItem = Electron.MenuItem;
import Menu = Electron.Menu;
import BrowserWindow = Electron.BrowserWindow;
import {Plot, PlotType} from "../model/Plot";
import * as ElectronStorage from "electron-json-storage";

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
    private _rootClass: string;
    private _runFunction: string;
    private _menu: Menu;

    static SAVE_KEY_PREFIX: string = "TEMPO-";
    plots: Collections.Dictionary<string, { variables: string[], type: PlotType }>;
    onLoad: (cfg: Configuration) => void;

    constructor() {
        // Create configuraton tab in menu
        this.plots = new Collections.Dictionary<string, { variables: string[], type: PlotType }>()
        this.setupMenu();
    }

    loadByRootClass(root: string) {
        var self = this;
        ElectronStorage.get(self.getSaveKeyFromRootClass(root), (error: any, data: any) => {
            if (error) {
                throw error;
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

            console.log(self);
        });
    }

    save() {
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
            ElectronStorage.set(this.getSaveKeyFromRootClass(this.rootClass), json, (error) => {
                if (error) {
                    throw error;
                }
            });
        }
    }

    addPlot(title: string, variables: string[], type: PlotType) {
        this.plots.setValue(title, { variables, type });
    }

    setupMenu() {
        let self = this;

        ElectronStorage.keys((error, keys) => {
            if (error) {
                throw error;
            }

            // Filter: Only look at saved configurations
            var keysFiltered = keys.filter(self.isSaveKeyRootClass);

            // Create rootclass submenu
            var rootClasses = keysFiltered.map((key) => {
                return {
                    label: self.getRootClassFromSaveKey(key),
                    submenu: [
                        {
                            label: "Load",
                            sublabel: self.getRootClassFromSaveKey(key),
                            click: self.onClickLoad.bind(self)
                        },
                        {
                            label: "Remove",
                            sublabel: self.getRootClassFromSaveKey(key),
                            click: self.onClickRemove.bind(self)
                        }
                    ]
                };
            });
            // Create template
            var template = <MenuItemOptions[]>menuTemplate;
            template.unshift({
                label: 'Config',
                submenu: [
                    {
                        label: 'Recent',
                        submenu: rootClasses
                    },
                    {
                        label: 'Save',
                        accelerator: 'CmdOrCtrl+S',
                        click() {
                            self.save();
                        }
                    }
                ]
            });
            // Setup menu
            self._menu = electron.remote.Menu.buildFromTemplate(template);
            electron.remote.Menu.setApplicationMenu(self._menu);
        });
    }

    onClickLoad(item: MenuItem, window: BrowserWindow) {
        let rootClass = item.sublabel;
        this.loadByRootClass(rootClass);
    }

    onClickRemove(item: MenuItem, window: BrowserWindow) {
        let self = this;
        let saveKey = this.getSaveKeyFromRootClass(item.sublabel);
        ElectronStorage.remove(saveKey, (error) => {
            if (error) {
                throw error;
            }

            // Remove the item
            let cfgMenu = <Menu>self._menu.items[0].submenu;
            let recentSubMenu = <Menu>cfgMenu.items[0].submenu;
            let index = recentSubMenu.items.findIndex((i) => i.label === item.sublabel);
            if(index !== undefined) {
                recentSubMenu.items[index].visible = false;
            }
        });
    }

    private getSaveKeyFromRootClass(rootClass: string): string {
        return Configuration.SAVE_KEY_PREFIX + rootClass;
    }

    private getRootClassFromSaveKey(rootClass: string): string {
        return rootClass.replace(Configuration.SAVE_KEY_PREFIX, "");
    }

    private isSaveKeyRootClass(saveKey: string): boolean {
        return saveKey.substr(0, 6) === Configuration.SAVE_KEY_PREFIX;
    }
}