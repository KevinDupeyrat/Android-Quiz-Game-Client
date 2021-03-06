package iut.paci.classroomcomunnity.frame;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import iut.paci.classroomcomunnity.R;
import iut.paci.classroomcomunnity.tools.ErrorServerTools;
import iut.paci.classroomcomunnity.tools.JsonTools;
import iut.paci.classroomcomunnity.activity.MainActivity;
import iut.paci.classroomcomunnity.activity.QuizActivity;
import iut.paci.classroomcomunnity.adapter.FriendAdapter;
import iut.paci.classroomcomunnity.bean.Amis;
import iut.paci.classroomcomunnity.tools.PropertiesTools;

/**
 * Fragment des amies qui va gérer
 * la liste des amis présent sur le server
 */
public class FriendFragment extends Fragment {


    private View rootView;
    private ProgressDialog progressDialog;
    private String jSonFriend = "";
    private String monNom;
    private String monPrenom;

    private static Timer timer_response;
    private static Timer timer_friend;
    private FriendAdapter adapter;
    private List<Amis> amisList;


    public FriendFragment() {}


    private static Timer getTimer_response() { return  timer_response; }

    private static Timer getTimer_friend() { return  timer_friend; }

    public static void delAllTimer() {

        delGetFriendTimer();
        delResonseTimer();
    }

    public static void delResonseTimer() {

        if (FriendFragment.getTimer_response() != null) {
            FriendFragment.getTimer_response().cancel();
            FriendFragment.getTimer_response().purge();

        }

    }

    public static void delGetFriendTimer() {

        if (FriendFragment.getTimer_friend() != null) {
            FriendFragment.getTimer_friend().cancel();
            FriendFragment.getTimer_friend().purge();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Modification du titre
        getActivity().setTitle("Mes Amis");
        // On récupère l'instance de l'activité qui
        // contient ce Fragement
        rootView = inflater.inflate(R.layout.fragment_friend, container, false);

        // this.getLocalFriend();
        this.getRemoteFriend();

        this.refreshFriends();
        this.verifHaveRequest();

        return rootView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        delAllTimer();
    }

    /**
     * Méthode qui permet de rafraichir la liste
     * d'amis avec les éventulles nouvelles connexions et les
     * déconnexions
     */
    public void refreshFriends() {

        timer_friend = new Timer();
        timer_friend.schedule(new TimerTask() {

            @Override
            public void run() {

        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                //paci.iut.1235
                .load(PropertiesTools.genURL(getContext(), "friends")
                        +"?key="+ MainActivity.getServerCode())
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {


                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(ErrorServerTools.errorManager(response, getContext(), getActivity())){
                                jSonFriend = response.getResult();
                                amisList = JsonTools.getAmis(jSonFriend);

                                // On efface l'adapter
                                adapter.clear();
                                // On rajoute la nouvelle liste
                                adapter.addAll(amisList);
                                // On notifie du chagement
                                adapter.notifyDataSetChanged();
                            }
                        }

                    }
                });
            }
        }, 4000, 4000);


    }


    /**
     * Méthode qui permet d'aller chercher la liste
     * d'amis sur le server
     */
    private void getRemoteFriend() {

        // Initialisation de la bar de progression
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage(getString(R.string.wait_friend));
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();


        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                //paci.iut.1235
                .load(PropertiesTools.genURL(getContext(), "friends")
                        +"?key="+ MainActivity.getServerCode())
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        // Une fois la requête terminé nous
                        // fermons la bar de progression
                        progressDialog.dismiss();

                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(ErrorServerTools.errorManager(response, getContext(), getActivity())){
                                jSonFriend = response.getResult();
                                initListView();
                            }
                        }

                    }
                });
    }


    /**
     * Méthode de création de la ListView
     */
    private void initListView() {

        // On récupère la listeView
        ListView listView = (ListView) rootView.findViewById(R.id.listView);

        // On utilise la class JsonTools pour récupérer la
        // liste d'amis à partir de la String Json
        amisList = JsonTools.getAmis(this.jSonFriend);


        // Mise à jour du Navigator View
        CircleImageView avatar = (CircleImageView) rootView.findViewById(R.id.avatar);
        TextView avatarName = (TextView) MainActivity.getNavigatView().findViewById(R.id.mainAvatarName);
        TextView prenomInitial = (TextView) MainActivity.getNavigatView().findViewById(R.id.prenomInitial);
        TextView nomInitial = (TextView) MainActivity.getNavigatView().findViewById(R.id.nomInitial);


        for(Amis amis: amisList)
            if(amis.getId() == MainActivity.getMy_id()) {
                monPrenom = amis.getPrenom();
                monNom = amis.getNom();
                avatarName.setText(monNom + " " + monPrenom);
                prenomInitial.setText(String.valueOf(monPrenom.charAt(0)));
                nomInitial.setText(String.valueOf(monNom.charAt(0)));
            }


        // On crée l'adapter pour la listeView
        adapter = new FriendAdapter(rootView.getContext(), R.layout.item_player, amisList);

        // On crée un listener sur chaque bouton
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Amis ami = (Amis) adapterView.getItemAtPosition(position);

                // On vérifi que l'utilistateur est connecté
                // S'il ne l'ai pas on affiche un message à l'utilisateur
                if(ami.isPresent() == 0){

                    Toast.makeText(getContext(), "Désolé mais " + ami.getNom() + " "
                            + ami.getPrenom() + " n'est pas connecté :(", Toast.LENGTH_SHORT).show();
                } else {

                    // Initialisation de la bar de progression
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage(getString(R.string.wait_answer_request));
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    //TODO: Envoi de la requête à l'amis
                    pushFriendRequest(ami);
                   // goToQuiz(ami);

                }
            }
        });

        // Nous ajoutons notre adapter à notre listView
        listView.setAdapter(adapter);
    }


    /**
     * Méthode qui va permettre d'envoyer une requête à
     * l'ami séléctionné
     */
    private void pushFriendRequest(final Amis amis) {


        this.progressDialog.dismiss();
        // Initialisation de la bar de progression
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage(getString(R.string.wait_friend));
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();

        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                //paci.iut.1235
                .load(PropertiesTools.genURL(getContext(), "put_request_friend")
                        +"?key="+ MainActivity.getServerCode()
                        +"&my_id=" + MainActivity.getMy_id()
                        +"&friend_id=" + amis.getId())
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        // Une fois la requête terminé nous
                        // fermons la bar de progression
                        //progressDialog.dismiss();

                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(ErrorServerTools.errorManager(response, getContext(), getActivity())){

                                delResonseTimer();
                                timer_response = new Timer();
                                timer_response.schedule(new TimerTask() {

                                    @Override
                                    public void run() {

                                        verifFriendAsRespondedRequest(amis);

                                    }
                                }, 1000, 1000);
                            }
                        }

                    }
                });
    }


    /**
     * Méthode qui permet la vérification ponctuelle
     * que l'amis a repondu à la requête
     */
    private void verifFriendAsRespondedRequest(final Amis amis) {

        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                //paci.iut.1235
                .load(PropertiesTools.genURL(getContext(), "get_response_friend")
                        +"?key="+ MainActivity.getServerCode()
                        +"&my_id=" + MainActivity.getMy_id())
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {



                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(ErrorServerTools.errorManager(response, getContext(), getActivity())){

                                // Une fois la requête terminé nous
                                // fermons la bar de progression
                                progressDialog.dismiss();

                                if(response.getResult().equals("yes")) {
                                    delResonseTimer();
                                    goToQuiz(amis);
                                } else if(response.getResult().equals("no")) {
                                    //TODO: Gérer une reponse négative
                                    System.out.println("ON A DIT NON !!!!");
                                    delResonseTimer();
                                    Toast.makeText(getContext(), "Ton amis a refusé de jouer", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }

                    }
                });

    }


    /**
     * Méthode qui permet de savoir si nous avons une
     * requête qui nous attend sur le server.
     * Cette méthode est effecter en arrière plan de
     * façon ponctuelle.
     * Cette méthode est lancer à la création de du fragment
     */
    public void verifHaveRequest() {

        timer_response = new Timer();
        timer_response.schedule(new TimerTask() {

            @Override
            public void run() {

                loopVerifHaveRequest();

            }
        }, 1000, 1000);


    }


    /**
     * Méthode qui tourne en tache de fond pour la
     * vérification de requête sur le server
     */
    private void loopVerifHaveRequest() {

        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                .load(PropertiesTools.genURL(getContext(), "ckeck_request")
                        +"?key="+ MainActivity.getServerCode()
                        +"&my_id=" + MainActivity.getMy_id())
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, final Response<String> response) {

                        // Une fois la requête terminé nous
                        // fermons la bar de progression
                        // progressDialog.dismiss();

                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(ErrorServerTools.errorManager(response, getContext(), getActivity())){


                                if(!response.getResult().equals("please wait")){

                                    delResonseTimer();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Tu veux jouer avec moi ?");
                                    builder.setMessage("Vous avez reçu une requête de la part de " + response.getResult());
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int id) {

                                            pushAnswerToRequest(response.getResult(),"yes");

                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int id) {

                                            pushAnswerToRequest(response.getResult(),"no");

                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();

                                }


                            }
                        }

                    }
                });


    }


    /**
     * Méthode qui permet de renvoyer la reponse (true or false)
     * au server
     */
    private void pushAnswerToRequest(final String friend_id, final String answer) {

        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                //paci.iut.1235
                .load(PropertiesTools.genURL(getContext(), "put_responce_request_friend")
                        +"?key="+ MainActivity.getServerCode()
                        +"&my_id=" + MainActivity.getMy_id()
                        +"&friend_id=" + friend_id
                        +"&response=" + answer)
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        // Une fois la requête terminé nous
                        // fermons la bar de progression
                        progressDialog.dismiss();

                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(ErrorServerTools.errorManager(response, getContext(), getActivity())){

                                if(answer.equals("yes")) {

                                    timer_response.cancel();
                                    timer_response.purge();

                                    for (Amis amis : amisList) {
                                        if (String.valueOf(amis.getId()).equals(friend_id)) {
                                            goToQuiz(amis);
                                            System.out.println(amis.getPrenom());
                                        }
                                    }
                                }

                            }
                        }

                    }
                });

    }


    /**
     * Méthode qui permet de lancer
     * le Quiz avec l'ami séléctionné
     * @param ami
     */
    private void goToQuiz(final Amis ami){

        delAllTimer();
//        this.logOutServer();

        // Nous récupérons toutes les info que
        // nous avons besoin pour la suite
        String nom = ami.getNom();
        String prenom = ami.getPrenom();
        int id  = ami.getId();

        // Création d'un Intent (activité)
        Intent intent = new Intent(getContext(), QuizActivity.class);
        // Création d'une boite
        Bundle bundle = new Bundle();
        // Ajout de l'identifiant dans notre boite
        bundle.putString("MonNom",monNom);
        bundle.putString("MonPrenom",monPrenom);

        bundle.putInt("id",id);
        bundle.putString("nom",nom);
        bundle.putString("prenom",prenom);
        bundle.putInt("score1",0);
        bundle.putInt("score2",0);
        // Ajout de notre boite dans notre prochaine activité
        intent.putExtras(bundle);
        //intent.putExtra("fragmentActivity",getActivity());
        // On demarre une activité

        // On redirige en suite vers la page principale avant d'ouvrir le quiz
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.contentFrame, new HomeFragment()).commit();

        startActivity(intent);

    }


//    /**
//     * Méthode qui permet de ce déloger du server
//     */
//    private void logOutServer(){
//
//        // On envoie la demande de connexion
//        // au serveur
//        Ion.with(getContext())
//                .load( PropertiesTools.genURL(getContext(), "logout")
//                        +"?key=" + MainActivity.getServerCode()
//                        +"&id="+ MainActivity.getMy_id())
//                .asJsonObject()
//                .setCallback(new FutureCallback<JsonObject>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonObject result) {
//
//                    }
//                });
//    }

}
