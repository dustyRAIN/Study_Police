from django.shortcuts import render
from django.http import HttpResponse
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import JSONParser, MultiPartParser, FormParser
from .models import CustomUser, EmailByProvider
from .serializers import DemoPhotoSerializer, RegisterWOPassSerializer

from rest_framework import permissions

from database_study_police.settings import SOCIAL_AUTH_GOOGLE_OAUTH2_KEY

from rest_auth.registration.views import RegisterView
from .utils import login_user, set_email_taken_by_provider
from classes.utils import randomStringDigits

from google.oauth2 import id_token
from google.auth.transport import requests

from django.utils.decorators import method_decorator
from django.views.decorators.csrf import csrf_exempt





class AccountExistsAPICall(APIView):

    '''
    Defines get method to check whether an email already exists with an account.
    '''

    def get(self, request, email):

        '''
        This method returns whether the email already exists or not.

        additional @param email
        '''

        result = None
        
        if email is not None:
            result = CustomUser.objects.filter(email = email)

        if result is not None and len(result) == 1:
            return Response({'data':{'exists' : 1}}, status=status.HTTP_200_OK)

        return Response({'data':{'exists' : 0}}, status=status.HTTP_200_OK)




class CustomRegisterView(RegisterView):
    queryset = CustomUser.objects.all()


class ResigterWOPassword(RegisterView):

    '''
    Enables the provider to create a user in the database with a random password
    '''

    queryset = CustomUser.objects.all()
    permission_classes = [permissions.AllowAny, ]

    @method_decorator(csrf_exempt)
    def dispatch(self, *args, **kwargs):
        return super(RegisterView, self).dispatch(*args, **kwargs)

    def create(self, request, *args, **kwargs):
        print(request.data)
        serializer = RegisterWOPassSerializer(data = request.data)
        serializer.is_valid(raise_exception=True)
        print(serializer.data)
        user = self.perform_create(serializer)
        headers = self.get_success_headers(serializer.data)

        return Response(self.get_response_data(user),
                        status=status.HTTP_201_CREATED,
                        headers=headers)

    


class UploadDemoPhoto(APIView):

    parser_classes = [MultiPartParser, JSONParser, FormParser]

    def post(self, request, *args, **kwargs):
        serializer = DemoPhotoSerializer(data = request.data)


        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status = status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status = status.HTTP_400_BAD_REQUEST)


class GoogleCallback(APIView):

    def post(self, request):
        print(request.data)
        pass


class GoogleLoginFromToken(APIView):

    '''
    Defines post method to validate Google id token.
    '''

    def post(self, request, *args, **kwargs):
        token = request.data['token']
        CLIENT_ID = SOCIAL_AUTH_GOOGLE_OAUTH2_KEY

        try:
            # Specify the CLIENT_ID of the app that accesses the backend:
            idinfo = id_token.verify_oauth2_token(token, requests.Request(), CLIENT_ID)

            # Or, if multiple clients access the backend server:
            # idinfo = id_token.verify_oauth2_token(token, requests.Request())
            # if idinfo['aud'] not in [CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3]:
            #     raise ValueError('Could not verify audience.')

            if idinfo['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                raise ValueError('Wrong issuer.')
            
            # If auth request is from a G Suite domain:
            # if idinfo['hd'] != GSUITE_DOMAIN_NAME:
            #     raise ValueError('Wrong hosted domain.')

            # ID token is valid. Get the user's Google Account ID from the decoded token.
            userid = idinfo['sub']

            if idinfo['email'] is None:
                return Response({"error" : "Google account is private. Can't sign in with this account."}, status = status.HTTP_200_OK)
            
            email = idinfo['email']

            print(email)

            if CustomUser.objects.filter(email = email).exists():
                return login_user(request, CustomUser.objects.get(email=email))

            else:
                req = request._request
                _mutable = req.POST._mutable
                req.POST._mutable = True
                req.POST['email'] = idinfo['email']
                req.POST['name'] = idinfo['name']
                req.POST['gender'] = 1
                password = randomStringDigits(8)
                req.POST['password1'] = password
                req.POST['password2'] = password
                del req.POST['token']
                req.POST._mutable = _mutable

                
                response = ResigterWOPassword.as_view()(request = req)
                


                if CustomUser.objects.filter(email = req.POST['email']).exists():
                    user = CustomUser.objects.get(email = req.POST['email'])
                    trial = 50
                    while  trial > 0 and not set_email_taken_by_provider(user.id, 1):
                        trial = trial - 1 
                    
                    if trial == 0:
                        CustomUser.objects.get(email = req.POST['email']).delete()
                        return Response({"error": "Server error"})

                    else:
                        return response

                else:
                    return Response({"error": "Server error, couldn't create user"})
                    

        except ValueError:
            return Response({"error": "Invalid Token."})
            pass


class ProviderTakenEmail(APIView):

    def get(self, request):
        email = request.data['email']

        if email is not None:
            user = CustomUser.objects.get(email = email)
            if EmailByProvider.objects.filter(email = user.id).exists():
                return Response({'exists' : 1}, status=status.HTTP_200_OK)
            else:
                return Response({'exists' : 2}, status=status.HTTP_200_OK)

        else:
            return Response({"error": "bad request"}, status=status.HTTP_400_BAD_REQUEST)


