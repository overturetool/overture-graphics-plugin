import {SubscriptionClient} from "../protocol/SubscriptionClient";
import {Navigation} from "../Navigation";

export class RootClassPickerController {
    private navigation: Navigation;
    private client: SubscriptionClient;
    private form: W2UI.W2Form;

    constructor(client: SubscriptionClient, navigation: Navigation) {
        this.client = client;
        this.navigation = navigation;
    }

    async didMount() {
        if(this.form != null) {
            w2ui["rcPicker"].render($('#rcPicker')[0]);
            return;
        }

        let self = this;
        let model : Array<string> = await this.client.getClassInfo();

        this.form = $('#rcPicker').w2form({
            name: "rcPicker",
            header: 'Choose root class',
            fields: [
                { name: 'field_list', type: 'list', required: true,
                    options: { items: model } }
            ],
            actions: {
                reset: function () {
                    this.clear();
                },
                save: async function () {
                    var className = this.record["field_list"].text;
                    var response: string = await self.client.setRootClass(className);

                    if(response === "OK") {
                        this.clear();
                        self.navigation.openRunFunctionPickerView();
                    }
                }
            }
        });
    }
}