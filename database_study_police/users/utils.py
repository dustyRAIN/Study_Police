from django.contrib.auth import login
from rest_framework.response import Response
from rest_framework import status
from .models import EmailByProvider
from .serializers import EmailTakenSerializer
from django.core.files import uploadedfile

def login_user(request, user):
    user.backend = 'django.contrib.auth.backends.ModelBackend'
    login(request, user)
    return Response({"key" : "fake_token___sad"}, status = status.HTTP_200_OK)


def set_email_taken_by_provider(email, provider):
    if email and provider:
        serializer = EmailTakenSerializer(data={'email': email, 'provider': provider})

        if serializer.is_valid():
            serializer.save()
            return True

        else:
            print(serializer.errors)
            return False

    else:
        return False
