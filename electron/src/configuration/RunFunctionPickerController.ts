import {SubscriptionClient} from "../protocol/SubscriptionClient";
import {Navigation} from "../Navigation";

export class RunFunctionPickerController {
    private navigation: Navigation;
    private client: SubscriptionClient;
    private form: W2UI.W2Form;

    constructor(client: SubscriptionClient, navigation: Navigation) {
        this.client = client;
        this.navigation = navigation;
    }

    async didMount() {
        if(this.form != null) {
            w2ui["rfPicker"].render($('#rfPicker')[0]);
            return;
        }

        let self = this;
        let model : Array<string> = await this.client.getFunctionInfo();

        model;
        this.form = $('#rfPicker').w2form({
            name: "rfPicker",
            header: 'Choose which method to run',
            fields: [
                { name: 'field_list', type: 'list', required: true,
                    options: { items: model } }
            ],
            actions: {
                reset: function () {
                    this.clear();
                },
                save: async function () {
                    var fct = this.record["field_list"].text;
                    self.client.setRunFunction(fct);
                    this.clear();
                    self.navigation.openAddPlotView();
                }
            }
        });
    }
}